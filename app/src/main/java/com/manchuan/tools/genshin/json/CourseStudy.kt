package com.manchuan.tools.genshin.json


import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourseStudy(
    @SerialName("data")
    var `data`: Data,
    @SerialName("message")
    var message: String,
    @SerialName("retcode")
    var retcode: Int,
) {
    @Serializable
    data class Data(
        @SerialName("posts")
        var posts: List<Post>,
    ) {
        @Serializable
        data class Post(
            @SerialName("collection")
            var collection: Collection,
            @SerialName("cover")
            var cover: Cover,
            @SerialName("forum")
            var forum: Forum,
            @Contextual
            @SerialName("forum_rank_info")
            var forumRankInfo: Any?,
            @SerialName("help_sys")
            var helpSys: HelpSys,
            @SerialName("hot_reply_exist")
            var hotReplyExist: Boolean,
            @SerialName("image_list")
            var imageList: List<Image>,
            @SerialName("is_block_on")
            var isBlockOn: Boolean,
            @SerialName("is_official_master")
            var isOfficialMaster: Boolean,
            @SerialName("is_user_master")
            var isUserMaster: Boolean,
            @SerialName("last_modify_time")
            var lastModifyTime: Int,
            @SerialName("post")
            var post: Post,
            @SerialName("recommend_type")
            var recommendType: String,
            @SerialName("self_operation")
            var selfOperation: SelfOperation,
            @SerialName("stat")
            var stat: Stat,
            @SerialName("topics")
            var topics: List<Topic>,
            @SerialName("user")
            var user: User,
            @SerialName("vod_list")
            var vodList: List<Vod>,
            @SerialName("vote_count")
            var voteCount: Int,
        ) {
            @Serializable
            data class Collection(
                @SerialName("collection_id")
                var collectionId: String,
                @SerialName("collection_title")
                var collectionTitle: String,
                @SerialName("cur")
                var cur: Int,
                @SerialName("next_post_game_id")
                var nextPostGameId: Int,
                @SerialName("next_post_id")
                var nextPostId: String,
                @SerialName("next_post_view_type")
                var nextPostViewType: Int,
                @SerialName("prev_post_game_id")
                var prevPostGameId: Int,
                @SerialName("prev_post_id")
                var prevPostId: String,
                @SerialName("prev_post_view_type")
                var prevPostViewType: Int,
                @SerialName("total")
                var total: Int,
            )

            @Serializable
            data class Cover(
                @SerialName("crop")
                var crop: Crop,
                @SerialName("entity_id")
                var entityId: String,
                @SerialName("entity_type")
                var entityType: String,
                @SerialName("format")
                var format: String,
                @SerialName("height")
                var height: Int,
                @SerialName("image_id")
                var imageId: String,
                @SerialName("is_user_set_cover")
                var isUserSetCover: Boolean,
                @SerialName("size")
                var size: String,
                @SerialName("url")
                var url: String,
                @SerialName("width")
                var width: Int,
            ) {
                @Serializable
                data class Crop(
                    @SerialName("h")
                    var h: Int,
                    @SerialName("url")
                    var url: String,
                    @SerialName("w")
                    var w: Int,
                    @SerialName("x")
                    var x: Int,
                    @SerialName("y")
                    var y: Int,
                )
            }

            @Serializable
            data class Forum(
                @SerialName("forum_cate")
                @Contextual
                var forumCate: Any?,
                @SerialName("game_id")
                var gameId: Int,
                @SerialName("icon")
                var icon: String,
                @SerialName("id")
                var id: Int,
                @SerialName("name")
                var name: String,
            )

            @Serializable
            data class HelpSys(
                @SerialName("answer_num")
                var answerNum: Int,
                @Contextual
                @SerialName("top_up")
                var topUp: Any?,
            )

            @Serializable
            data class Image(
                @Contextual
                @SerialName("crop")
                var crop: Any?,
                @SerialName("entity_id")
                var entityId: String,
                @SerialName("entity_type")
                var entityType: String,
                @SerialName("format")
                var format: String,
                @SerialName("height")
                var height: Int,
                @SerialName("image_id")
                var imageId: String,
                @SerialName("is_user_set_cover")
                var isUserSetCover: Boolean,
                @SerialName("size")
                var size: String,
                @SerialName("url")
                var url: String,
                @SerialName("width")
                var width: Int,
            )

            @Serializable
            data class Post(
                @SerialName("cate_id")
                var cateId: Int,
                @SerialName("content")
                var content: String,
                @SerialName("cover")
                var cover: String,
                @SerialName("created_at")
                var createdAt: Int,
                @SerialName("deleted_at")
                var deletedAt: Int,
                @SerialName("f_forum_id")
                var fForumId: Int,
                @SerialName("game_id")
                var gameId: Int,
                @SerialName("images")
                var images: List<String>,
                @SerialName("is_deleted")
                var isDeleted: Int,
                @SerialName("is_in_profit")
                var isInProfit: Boolean,
                @SerialName("is_interactive")
                var isInteractive: Boolean,
                @SerialName("is_original")
                var isOriginal: Int,
                @SerialName("is_profit")
                var isProfit: Boolean,
                @SerialName("max_floor")
                var maxFloor: Int,
                @SerialName("post_id")
                var postId: String,
                @SerialName("post_status")
                var postStatus: PostStatus,
                @SerialName("pre_pub_status")
                var prePubStatus: Int,
                @SerialName("reply_time")
                var replyTime: String,
                @SerialName("republish_authorization")
                var republishAuthorization: Int,
                @SerialName("review_id")
                var reviewId: Int,
                @SerialName("structured_content")
                var structuredContent: String,
                @SerialName("subject")
                var subject: String,
                @SerialName("topic_ids")
                var topicIds: List<Int>,
                @SerialName("uid")
                var uid: String,
                @SerialName("updated_at")
                var updatedAt: Int,
                @SerialName("view_status")
                var viewStatus: Int,
                @SerialName("view_type")
                var viewType: Int,
            ) {
                @Serializable
                data class PostStatus(
                    @SerialName("is_good")
                    var isGood: Boolean,
                    @SerialName("is_official")
                    var isOfficial: Boolean,
                    @SerialName("is_top")
                    var isTop: Boolean,
                )
            }

            @Serializable
            data class SelfOperation(
                @SerialName("attitude")
                var attitude: Int,
                @SerialName("is_collected")
                var isCollected: Boolean,
            )

            @Serializable
            data class Stat(
                @SerialName("bookmark_num")
                var bookmarkNum: Int,
                @SerialName("forward_num")
                var forwardNum: Int,
                @SerialName("like_num")
                var likeNum: Int,
                @SerialName("reply_num")
                var replyNum: Int,
                @SerialName("view_num")
                var viewNum: Int,
            )

            @Serializable
            data class Topic(
                @SerialName("content_type")
                var contentType: Int,
                @SerialName("cover")
                var cover: String,
                @SerialName("game_id")
                var gameId: Int,
                @SerialName("id")
                var id: Int,
                @SerialName("is_good")
                var isGood: Boolean,
                @SerialName("is_interactive")
                var isInteractive: Boolean,
                @SerialName("is_top")
                var isTop: Boolean,
                @SerialName("name")
                var name: String,
            )

            @Serializable
            data class User(
                @SerialName("avatar")
                var avatar: String,
                @SerialName("avatar_url")
                var avatarUrl: String,
                @SerialName("certification")
                var certification: Certification,
                @SerialName("gender")
                var gender: Int,
                @SerialName("introduce")
                var introduce: String,
                @SerialName("is_followed")
                var isFollowed: Boolean,
                @SerialName("is_following")
                var isFollowing: Boolean,
                @SerialName("level_exp")
                var levelExp: LevelExp,
                @SerialName("nickname")
                var nickname: String,
                @SerialName("pendant")
                var pendant: String,
                @SerialName("uid")
                var uid: String,
            ) {
                @Serializable
                data class Certification(
                    @SerialName("label")
                    var label: String,
                    @SerialName("type")
                    var type: Int,
                )

                @Serializable
                data class LevelExp(
                    @SerialName("exp")
                    var exp: Int,
                    @SerialName("level")
                    var level: Int,
                )
            }

            @Serializable
            data class Vod(
                @SerialName("cover")
                var cover: String,
                @SerialName("duration")
                var duration: Int,
                @SerialName("id")
                var id: String,
                @SerialName("resolutions")
                var resolutions: List<Resolution>,
                @SerialName("review_status")
                var reviewStatus: Int,
                @SerialName("transcoding_status")
                var transcodingStatus: Int,
                @SerialName("view_num")
                var viewNum: Int,
            ) {
                @Serializable
                data class Resolution(
                    @SerialName("bitrate")
                    var bitrate: Int,
                    @SerialName("definition")
                    var definition: String,
                    @SerialName("format")
                    var format: String,
                    @SerialName("height")
                    var height: Int,
                    @SerialName("label")
                    var label: String,
                    @SerialName("size")
                    var size: String,
                    @SerialName("url")
                    var url: String,
                    @SerialName("width")
                    var width: Int,
                )
            }
        }
    }
}